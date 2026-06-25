import { useState } from 'react';

const useToast = () => {

  const [notification, setNotification] = useState({
    show: false,
    message: '',
    icon: 'info-circle'
  });

  const showToast = (
    message,
    icon = 'info-circle'
  ) => {

    setNotification({
      show: true,
      message,
      icon
    });

    setTimeout(() => {
      setNotification(prev => ({
        ...prev,
        show: false
      }));
    }, 2500);
  };

  return {
    notification,
    showToast
  };
};

export default useToast;