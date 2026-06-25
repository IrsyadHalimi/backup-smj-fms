export const formatIndoDate = (dateString) => {
  if (!dateString || dateString === "-") return "-";

  try {
    const date = new Date(dateString);

    if (isNaN(date.getTime())) return dateString;

    return new Intl.DateTimeFormat('id-ID', {
      day: 'numeric',
      month: 'long',
      year: 'numeric'
    }).format(date);

  } catch (e) {
    return dateString;
  }
};