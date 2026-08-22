import { Navigate, redirect, type RouteObject } from 'react-router-dom'
import GuesthouseDetailPage from '../features/accommodation/GuesthouseDetailPage'
import GuesthouseListPage from '../features/accommodation/GuesthouseListPage'
import AdminBookingDetail from '../features/admin/bookings/AdminBookingDetail'
import AdminBookingQueue from '../features/admin/bookings/AdminBookingQueue'
import AdminLoginPage from '../features/admin/auth/AdminLoginPage'
import AdminGuesthouseContentEditor from '../features/admin/content/AdminGuesthouseContentEditor'
import AdminTourismPage from '../features/admin/tourism/AdminTourismPage'
import ProtectedAdminRoute from '../features/admin/auth/ProtectedAdminRoute'
import AdminShell from '../features/admin/layout/AdminShell'
import BookingPage from '../features/booking/BookingPage'
import BookingManagementPage from '../features/booking/BookingManagementPage'
import BookingRequestSuccessPage from '../features/booking/BookingRequestSuccessPage'
import TourismMapPage from '../features/tourism/TourismMapPage'
import { readPreferredLanguage } from '../i18n/languages'
import ApplicationErrorPage from './ApplicationErrorPage'
import LanguageLayout from './LanguageLayout'
import NotFoundPage from './NotFoundPage'

export const appRoutes: RouteObject[] = [
  {
    path: '/admin',
    element: <Navigate replace to="/admin/login" />,
  },
  {
    path: '/admin/login',
    element: <AdminLoginPage />,
    errorElement: <ApplicationErrorPage />,
  },
  {
    path: '/admin',
    element: <ProtectedAdminRoute />,
    errorElement: <ApplicationErrorPage />,
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
            element: <AdminBookingQueue />,
          },
          {
            path: 'bookings/:bookingId',
            element: <AdminBookingDetail />,
          },
          {
            path: 'content',
            element: <AdminGuesthouseContentEditor />,
          },
          {
            path: 'tourism',
            element: <AdminTourismPage />,
          },
        ],
      },
    ],
  },
  {
    path: '/',
    element: <></>,
    loader: () => redirect(`/${readPreferredLanguage()}`),
    errorElement: <ApplicationErrorPage />,
  },
  {
    path: '/:lang',
    element: <LanguageLayout />,
    errorElement: <ApplicationErrorPage />,
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
      {
        path: 'booking-management/:token',
        element: <BookingManagementPage />,
      },
      {
        path: 'star-tours',
        element: <TourismMapPage />,
      },
    ],
  },
  {
    path: '*',
    element: <NotFoundPage />,
  },
]
