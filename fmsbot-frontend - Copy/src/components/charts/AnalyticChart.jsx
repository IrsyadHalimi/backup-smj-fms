import React from 'react';
import Highcharts from 'highcharts';
import { HighchartsReact } from 'highcharts-react-official';

const AnalyticChart = ({ 
  type = 'column', 
  title = 'Analytic Chart', 
  categories = [], 
  series = [], 
  yAxisTitle = 'Fuel (Liter)' 
}) => {

  const options = {
    // Kunci utama background transparan ada di sini
    chart: {
      type: type,
      backgroundColor: 'transparent', 
      style: {
        fontFamily: 'Arial, sans-serif'
      }
    },
    title: {
      text: title,
      style: {
        color: '#ffffff' // Sesuaikan warna teks jika background dashboard gelap
      }
    },
    xAxis: {
      categories: categories,
      labels: {
        style: { color: '#cccccc' }
      },
      lineColor: '#555555'
    },
    yAxis: {
      title: {
        text: yAxisTitle,
        style: { color: '#cccccc' }
      },
      labels: {
        style: { color: '#cccccc' }
      },
      gridLineColor: 'rgba(255, 255, 255, 0.1)' // Garis grid tipis transparan
    },
    legend: {
      itemStyle: { color: '#ffffff' },
      itemHoverStyle: { color: '#aaaaaa' }
    },
    tooltip: {
      backgroundColor: 'rgba(33, 33, 33, 0.85)', // Tooltip semi-transparan
      style: { color: '#ffffff' },
      shared: true
    },
    plotOptions: {
      series: {
        borderWidth: 0,
        dataLabels: {
          enabled: true,
          color: '#ffffff'
        }
      }
    },
    // Menghilangkan watermark highcharts.com jika diinginkan
    credits: {
      enabled: false
    },
    series: series
  };

  return (
    <div className="chart-container" style={{ width: '100%', height: '400px' }}>
      <HighchartsReact
        highcharts={Highcharts}
        options={options}
      />
    </div>
  );
};

export default AnalyticChart;