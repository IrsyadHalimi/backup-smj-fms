import { useRef, useState } from 'react';

export const useDragScroll = () => {
  const dragRef = useRef(null);

  const [isDragging, setIsDragging] = useState(false);
  const [startX, setStartX] = useState(0);
  const [startY, setStartY] = useState(0);
  const [scrollLeft, setScrollLeft] = useState(0);
  const [scrollTop, setScrollTop] = useState(0);

  const handleMouseDown = (e) => {
    if (!dragRef.current) return;
    setIsDragging(true);
    // Mengamankan koordinat awal saat klik kiri mouse ditekan
    setStartX(e.pageX - dragRef.current.offsetLeft);
    setStartY(e.pageY - dragRef.current.offsetTop);
    setScrollLeft(dragRef.current.scrollLeft);
    setScrollTop(dragRef.current.scrollTop);
  };

  const handleMouseMove = (e) => {
    if (!isDragging || !dragRef.current) return;
    e.preventDefault();
    
    // Kalkulasi jarak pergeseran kursor
    const x = e.pageX - dragRef.current.offsetLeft;
    const y = e.pageY - dragRef.current.offsetTop;
    const walkX = (x - startX) * 1.5; // Multiplier 1.5x untuk kecepatan geser
    const walkY = (y - startY) * 1.5;

    dragRef.current.scrollLeft = scrollLeft - walkX;
    dragRef.current.scrollTop = scrollTop - walkY;
  };

  const handleMouseUpOrLeave = () => {
    setIsDragging(false);
  };

  return {
    dragRef,
    isDragging,
    handleMouseDown,
    handleMouseMove,
    handleMouseUpOrLeave
  };
};