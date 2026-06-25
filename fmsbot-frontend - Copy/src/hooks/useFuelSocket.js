import useWebSocket from 'react-use-websocket';

export const useFuelSocket = ({
  WS_URL
}) => {
  const websocketHook =
    typeof useWebSocket === 'function'
      ? useWebSocket
      : (useWebSocket.default || useWebSocket.useWebSocket);

  const {
    lastJsonMessage,
    readyState
  } = websocketHook(WS_URL, {
    shouldReconnect: () => true,
    reconnectInterval: 3000
  });

  return {
    lastJsonMessage,
    readyState
  };
};