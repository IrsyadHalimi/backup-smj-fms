const isSecure =
  window.location.protocol === 'https:';

const hostName =
  '103.214.112.59';

const isLocal =
  hostName === '103.214.112.59' ||
  hostName === '103.214.112.59';

const backendHost =
  isLocal
    ? `${hostName}`
    : hostName;

export const BASE_URL =
  `${window.location.protocol}//${backendHost}`;

export const API_URL =
  `${BASE_URL}/api/fuel-transactions/`;

export const WS_URL =
  `${isSecure ? 'wss' : 'ws'}://${backendHost}/ws/fuel-sync/`;

export const AI_INSIGHT_URL = 
  `${BASE_URL}/api/ai/insight-router/`