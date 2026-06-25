const SortIcon = ({ config, col }) => {
  if (config.key !== col) {
    return (
      <i
        className="fas fa-sort"
        style={{ marginLeft: '5px', opacity: 0.2 }}
      ></i>
    );
  }

  return (
    <i
      className={`fas fa-sort-amount-${
        config.direction === 'asc' ? 'up' : 'down'
      }`}
      style={{
        marginLeft: '5px',
        color: '#ef4444'
      }}
    ></i>
  );
};

export default SortIcon;