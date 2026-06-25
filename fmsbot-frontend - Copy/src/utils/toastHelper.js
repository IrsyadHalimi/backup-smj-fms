export const showToastHelper = ({
  setNotification,
  msg,
  icon = "info-circle"
}) => {
  setNotification({
    show: true,
    message: msg,
    icon
  });

  setTimeout(() => {
    setNotification(prev => ({
      ...prev,
      show: false
    }));
  }, 2500);
};