import StatCard from './components/cards/StatCard';
import OCRStatusLabel from './components/tables/OCRStatusLabel';

import {
  SortIcon,
  thStyle,
  tdStyle,
  statusBadge
} from './components/tables/TableHelpers';

import {
  EXPORT_LAYOUT_KEY,
  AVAILABLE_FIELDS,
  menuButtonStyle,
  activeModeStyle,
  inactiveModeStyle
} from './utils/constants';

import { formatIndoDate } from './utils/dateFormatter';

import useToast from './hooks/useToast';

import {
  validateTransaction,
  reScreenTransaction
} from './services/validationService';

import "./styles/globalAnimations.css";

import DashboardCards from "./components/layout/DashboardCards";
import LeftMonitorPanel from "./components/layout/LeftMonitorPanel";
import RightAnalyticsPanel from "./components/layout/RightAnalyticsPanel";
import FuelTable from "./components/tables/FuelTable";
import OCRStatusLabel from "./components/tables/OCRStatusLabel";
import menuButtonStyle from "./styles/menuButtonStyle";

import { showToastHelper }
from "./utils/toastHelper";

import {
  handleOpenValidationHandler,
  handleSubmitValidationHandler,
  handleReScreeningHandler
}
from "./services/validation/validationHandlers";

function App() {

  const showToast = (msg, icon = "info-circle") => {
    showToastHelper({
      setNotification,
      msg,
      icon
    });
  };

  const handleOpenValidation = (record) => {
    handleOpenValidationHandler({
      record,

      setSelectedRecord,
      setFinalHM,
      setFinalFlow,

      setIsModalOpen,
      setActiveMenuId,

      showToast
    });
  };

  const handleSubmitValidation = async () => {

    await handleSubmitValidationHandler({

      BASE_URL,

      selectedRecord,

      finalHM,
      finalFlow,

      setDbHistory,
      setRecords,

      setIsModalOpen,
      setSelectedRecord,

      setFinalHM,
      setFinalFlow,

      fetchAllData,

      showToast
    });
  };

  const handleReScreening = async (record) => {

    await handleReScreeningHandler({

      BASE_URL,
      API_URL,

      record,

      setRecords,
      setDbHistory,

      setActiveMenuId,

      showToast
    });
  };

  return <FuelDashboard />;
}

<div style={modalOverlayStyle}></div>