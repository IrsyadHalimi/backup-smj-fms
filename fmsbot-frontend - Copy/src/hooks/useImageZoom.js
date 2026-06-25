import { useState } from 'react';
import { handleImageZoomCalc, getImageUrlCalc } from '../helpers/image/imageZoomHelper';

export const useImageZoom = (BASE_URL) => {
  const [zoomImage, setZoomImage] = useState(null);
  const [zoomPosition, setZoomPosition] = useState({ x: 0, y: 0 });
  const [sourceRect, setSourceRect] = useState(null);

  const getImageUrl = (path) => getImageUrlCalc(path, BASE_URL);

  const handleImageZoom = (e, imagePath, isLiveMode = false) => {
    // Jalankan kalkulasi dan update state lokal hook
    handleImageZoomCalc(e, imagePath, isLiveMode, BASE_URL, {
      setSourceRect,
      setZoomPosition,
      setZoomImage
    });
  };

  // Fungsi untuk membersihkan zoom (Close saat klik luar)
  const closeZoom = () => {
    setZoomImage(null);
    setSourceRect(null);
  };

  return {
    zoomImage,
    setZoomImage,
    zoomPosition,
    sourceRect,
    getImageUrl,
    handleImageZoom,
    closeZoom // 💡 Kita export fungsi ini untuk backdrop klik luar
  };
};