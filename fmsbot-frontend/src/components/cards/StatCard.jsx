const StatCard = ({
  title,
  value,
  color,
  activeTheme,
  subValue,
  value1label,
  value1,
  value2label,
  value2
}) => (
  <div
    style={{
      backgroundColor: activeTheme.card,
      padding: '12px 16px',
      borderRadius: '10px',
      borderStyle: 'solid',
      borderWidth: '1px 1px 1px 4px',
      borderColor: `${activeTheme.border} ${activeTheme.border} ${activeTheme.border} ${color}`,
      flex: 1,
      display: 'flex',
      flexDirection: 'column',
      justifyContent: 'center'
    }}
  >
    <div
      style={{
        fontSize: '12px',
        color: activeTheme.subText,
        fontWeight: 'bold',
        textTransform: 'uppercase',
        marginBottom: '4px'
      }}
    >
      {title}
    </div>

    <div
      style={{
        fontSize: '22px',
        fontWeight: '800',
        color: activeTheme.text,
        lineHeight: '1'
      }}
    >
      {value}
    </div>

    {subValue && (
      <div
        style={{
          fontSize: '10px',
          color: color,
          marginTop: '4px',
          fontWeight: 'bold'
        }}
      >
        {subValue}
      </div>
    )}
    
    {title == 'Total Fuel Liters' && (
      <>
      <div
        style={{
          flex: 1,
          display: 'flex',
          flexDirection: 'row',
          gap: '50px'
        }}>
        <div>
          <div
          style={{
            fontSize: '12px',
            color: activeTheme.subText,
            fontWeight: 'bold',
            textTransform: 'uppercase',
            marginBottom: '4px'
          }}
          >
            {value1label}
          </div>

          <div
            style={{
              fontSize: '18px',
              fontWeight: '800',
              color: activeTheme.text,
              lineHeight: '1'
            }}
          >
            {value1}
          </div>
        </div>
        <div>
          <div
          style={{
            fontSize: '12px',
            color: activeTheme.subText,
            fontWeight: 'bold',
            textTransform: 'uppercase',
            marginBottom: '4px'
          }}
          >
            {value2label}
          </div>

          <div
            style={{
              fontSize: '18px',
              fontWeight: '800',
              color: activeTheme.text,
              lineHeight: '1'
            }}
          >
            {value2}
          </div>
        </div>
      </div>
    </>
    )}
  </div>
);

export default StatCard;