import { useState, useEffect, useMemo, useRef, useCallback } from 'react';
import useWebSocketLib from 'react-use-websocket';

export const useFuelData = (BASE_URL, WS_URL, API_PATH) => {
  const [records, setRecords] = useState([]);
  const [dbHistory, setDbHistory] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isNewDataIn, setIsNewDataIn] = useState(false);
  const [notification, setNotification] = useState({ show: false, message: '', icon: 'info-circle' });
  
  const toastTimer = useRef(null);
  const fullApiPath = `${API_PATH}`;

  // Solusi Interoperabilitas Lib untuk Vite
  const websocketHook = typeof useWebSocketLib === 'function'
    ? useWebSocketLib
    : (useWebSocketLib.default || useWebSocketLib.useWebSocket);

  const showToast = useCallback((msg, icon = "info-circle") => {
    if (toastTimer.current) clearTimeout(toastTimer.current);
    setNotification({ show: true, message: msg, icon });
    toastTimer.current = setTimeout(() => {
      setNotification(prev => ({ ...prev, show: false }));
    }, 2500);
  }, []);

  const stats = useMemo(() => {
    const totalLiters = dbHistory.reduce((acc, curr) => acc + Number(curr.consumed_qty || 0), 0);
    const uniqueUnits = new Set(dbHistory.map(r => r.no_unit_sap)).size;
    const stations = new Set(dbHistory.map(r => r.gas_station)).size;
    const anomalies = dbHistory.filter(r => 
      r.screening_status === 'Mismatch' || r.screening_status?.includes('Error')
    ).length;
    return { totalLiters, uniqueUnits, anomalies, stations };
  }, [dbHistory]);

  const fetchHistory = async () => {
    setIsLoading(true);
    try {
      const response = await fetch(fullApiPath);
      if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
      const data = await response.json();
      setDbHistory(Array.isArray(data) ? data : (data.results || []));
    } catch (err) {
      console.error("Gagal load history:", err);
    } finally {
      setIsLoading(false);
    }
  };

  const fetchSingleRecord = async (id) => {
    try {
      const response = await fetch(`${fullApiPath}${id}/`);
      if (response.ok) return await response.json();
    } catch (err) {
      console.error("Error fetch detail:", err);
    }
    return null;
  };

  const { lastJsonMessage, readyState } = websocketHook(WS_URL, { 
    shouldReconnect: () => true, 
    reconnectInterval: 3000 
  });

  const updateAndMoveToTop = (prevArray, dataToDisplay) => {
    const filtered = prevArray.filter(item => item.id !== dataToDisplay.id);
    return [dataToDisplay, ...filtered];
  };

  const updateDataState = (prev, dataToDisplay, isLimitRecords = false) => {
    const isVerified = dataToDisplay.screening_status === 'Verified' || dataToDisplay.status === 'Verified';
    if (isVerified) return updateAndMoveToTop(prev, dataToDisplay);

    const isExist = prev.some(item => item.id === dataToDisplay.id);
    if (isExist) {
      return prev.map(item => item.id === dataToDisplay.id ? { ...item, ...dataToDisplay } : item);
    }

    if (!isLimitRecords) {
      setIsNewDataIn(true);
      setTimeout(() => setIsNewDataIn(false), 3000);
      showToast(`Data baru masuk dari unit: ${dataToDisplay.no_unit_sap || 'Unknown'}`, 'info-circle');
    }

    const baseArray = [dataToDisplay, ...prev];
    return isLimitRecords ? baseArray.slice(0, 10) : baseArray;
  };

  useEffect(() => {
    if (!lastJsonMessage?.data) return;
    const socketData = lastJsonMessage.data;

    fetchSingleRecord(socketData.id).then(fullData => {
      const dataToDisplay = fullData || { ...socketData };
      setDbHistory(prev => updateDataState(prev, dataToDisplay, false));
      setRecords(prev => updateDataState(prev, dataToDisplay, true));
    });
  }, [lastJsonMessage]);

  useEffect(() => { 
    fetchHistory(); 
    return () => { if (toastTimer.current) clearTimeout(toastTimer.current); };
  }, []);

  return {
    records, dbHistory, stats, isLoading, isNewDataIn,
    notification, showToast, isConnected: readyState === 1, fetchHistory
  };
};