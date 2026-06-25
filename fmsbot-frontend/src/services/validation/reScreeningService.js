export const reScreeningService = async ({
  BASE_URL,
  API_URL,
  record
}) => {

  const cleanBase = BASE_URL.trim();

  const cleanPath =
    API_URL
      .trim()
      .replace(/^\/+|\/+$/g, "");

  const urlObj = new URL(
    `${cleanPath}/${record.id}/re_screening/`,
    cleanBase
  );

  const targetUrl = urlObj.toString();

  console.log("🔥 URL YANG DIKIRIM:", targetUrl);

  const response = await fetch(targetUrl, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    }
  });

  return response;
};