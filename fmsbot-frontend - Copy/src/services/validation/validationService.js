export const submitValidationService = async ({
  BASE_URL,
  selectedRecord,
  finalHM,
  finalFlow
}) => {

  const hmToSubmit =
    finalHM ||
    selectedRecord.ai_hm_read ||
    selectedRecord.hm_km_unit;

  const flowToSubmit =
    finalFlow ||
    selectedRecord.ai_flow_read ||
    selectedRecord.flow_meter_value;

  const requestUrl =
    `${BASE_URL}/api/fuel-transactions/${selectedRecord.id}/validate/`
      .replace(/([^:]\/)\/+/g, "$1");

  const response = await fetch(requestUrl, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    },

    body: JSON.stringify({
      final_hm_value: String(hmToSubmit),
      final_flow_value: String(flowToSubmit),
      screening_status: 'Verified'
    })
  });

  return response;
};