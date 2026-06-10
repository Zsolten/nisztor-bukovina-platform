import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import './App.css'
import { AdminLayout } from './layouts/AdminLayout'
import { PublicLayout } from './layouts/PublicLayout'
import { AdminBookingsPage } from './pages/admin/AdminBookingsPage'
import { AdminContentPage } from './pages/admin/AdminContentPage'
import { AdminDashboardPage } from './pages/admin/AdminDashboardPage'
import { AdminLoginPage } from './pages/admin/AdminLoginPage'
import { AttractionsPage } from './pages/public/AttractionsPage'
import { BookingRequestPage } from './pages/public/BookingRequestPage'
import { DayTripsPage } from './pages/public/DayTripsPage'
import { GuesthousesPage } from './pages/public/GuesthousesPage'
import { HomePage } from './pages/public/HomePage'
import { ItineraryPlannerPage } from './pages/public/ItineraryPlannerPage'
import { NotFoundPage } from './pages/public/NotFoundPage'
import { PropertyDetailPage } from './pages/public/PropertyDetailPage'
import { RoomsPage } from './pages/public/RoomsPage'
import { LanguageGate } from './routing/LanguageGate'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/hu" replace />} />
        <Route path="/:lang" element={<LanguageGate />}>
          <Route element={<PublicLayout />}>
            <Route index element={<HomePage />} />
            <Route path="panzioink" element={<GuesthousesPage />} />
            <Route path="panzioink/:propertySlug" element={<PropertyDetailPage />} />
            <Route path="szobak" element={<RoomsPage />} />
            <Route path="latnivalok" element={<AttractionsPage />} />
            <Route path="csillagturak" element={<DayTripsPage />} />
            <Route path="foglalasi-keres" element={<BookingRequestPage />} />
            <Route path="programajanlo" element={<ItineraryPlannerPage />} />
          </Route>

          <Route path="admin" element={<AdminLayout />}>
            <Route index element={<AdminDashboardPage />} />
            <Route path="login" element={<AdminLoginPage />} />
            <Route path="foglalasok" element={<AdminBookingsPage />} />
            <Route path="tartalom" element={<AdminContentPage />} />
          </Route>
        </Route>
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
