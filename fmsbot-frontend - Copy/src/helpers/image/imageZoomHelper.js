export const getImageUrlCalc = (path, BASE_URL) => {
  if (!path) return null;
  if (String(path).startsWith('http')) return path;
  const cleanPath = String(path).startsWith('/') ? path : `/${path}`;
  return `${BASE_URL}${cleanPath}`;
};

export const handleImageZoomCalc = (e, imagePath, isLiveMode = false, BASE_URL, setters) => {
  if (!imagePath) return;
  
  // Ambil fungsi setter dari hook
  const { setSourceRect, setZoomPosition, setZoomImage } = setters;

  const rect = e.target.getBoundingClientRect();
  setSourceRect(rect);

  const zoomWidth = 350;
  const gap = 15; 

  let leftPos;

  if (isLiveMode) {
    leftPos = rect.right + gap;
    if (leftPos + zoomWidth > window.innerWidth) {
      leftPos = window.innerWidth - zoomWidth - 20;
    }
  } else {
    leftPos = rect.left - zoomWidth - gap;
    if (leftPos < 20) leftPos = 20;
  }

  let topPos = rect.top - 50; 
  if (topPos < 20) topPos = 20;
  
  if (topPos + 250 > window.innerHeight) {
    topPos = window.innerHeight - 270;
  }

  setZoomPosition({
    x: leftPos,
    y: topPos
  });

  setZoomImage(getImageUrlCalc(imagePath, BASE_URL));
};