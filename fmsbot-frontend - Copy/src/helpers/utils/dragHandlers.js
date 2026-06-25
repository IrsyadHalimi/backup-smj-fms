 // --- MOUSE DRAG HANDLERS ---
  export const handleMouseDown = (e) => {
    if (!dragRef.current) return;
    setIsDragging(true);
    setStartX(e.pageX - dragRef.current.offsetLeft);
    setStartY(e.pageY - dragRef.current.offsetTop);
    setScrollLeft(dragRef.current.scrollLeft);
    setScrollTop(dragRef.current.scrollTop);
  };

  export const handleMouseMove = (e) => {
    if (!isDragging || !dragRef.current) return;
    e.preventDefault();
    const x = e.pageX - dragRef.current.offsetLeft;
    const y = e.pageY - dragRef.current.offsetTop;
    const walkX = (x - startX) * 1.5; 
    const walkY = (y - startY) * 1.5;
    dragRef.current.scrollLeft = scrollLeft - walkX;
    dragRef.current.scrollTop = scrollTop - walkY;
  };

  export const handleMouseUpOrLeave = () => {
    setIsDragging(false);
  };