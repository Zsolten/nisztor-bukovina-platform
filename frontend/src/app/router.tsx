import { Navigate, redirect, type RouteObject } from 'react-router-dom'
import GuesthouseDetailPage from '../features/accommodation/GuesthouseDetailPage'
import GuesthouseListPage from '../features/accommodation/GuesthouseListPage'
import AdminBookingQueuePlaceholder from '../features/admin/bookings/AdminBookingQueuePlaceholder'
import AdminLoginPage from '../features/admin/auth/AdminLoginPage'
import ProtectedAdminRoute from '../features/admin/auth/ProtectedAdminRoute'
import AdminShell from '../features/admin/layout/AdminShell'
import BookingPage from '../features/booking/BookingPage'
import BookingRequestSuccessPage from '../features/booking/BookingRequestSuccessPage'
import { DEFAULT_LANGUAGE, readPreferredLanguage } from '../i18n/languages'
import LanguageLayout from './LanguageLayout'

export const appRoutes: RouteObject[] = [
  {
    path: '/admin',
    element: <Navigate replace to="/admin/login" />,
  },
  {
    path: '/admin/login',
    element: <AdminLoginPage />,
  },
  {
    path: '/admin',
    element: <ProtectedAdminRoute />,
    children: [
      {
        element: <AdminShell />,
        children: [
          {
            index: true,
            element: <Navigate replace to="/admin/bookings" />,
          },
          {
            path: 'bookings',
            element: <AdminBookingQueuePlaceholder />,
          },
        ],
      },
    ],
  },
  {
    path: '/',
    element: <></>,
    loader: () => redirect(`/${readPreferredLanguage()}`),
  },
  {
    path: '/:lang',
    element: <LanguageLayout />,
    children: [
      {
        index: true,
        element: <GuesthouseListPage />,
      },
      {
        path: 'guesthouses/:slug',
        element: <GuesthouseDetailPage />,
      },
      {
        path: 'booking',
        element: <BookingPage />,
      },
      {
        path: 'guesthouses/:slug/booking',
        element: <BookingPage />,
      },
      {
        path: 'guesthouses/:slug/booking/review',
        element: <BookingPage />,
      },
      {
        path: 'booking-request-success',
        element: <BookingRequestSuccessPage />,
      },
    ],
  },
  {
    path: '*',
    element: <Navigate to={`/${DEFAULT_LANGUAGE}`} replace />,
  },
]
