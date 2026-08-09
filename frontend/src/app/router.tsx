import { Navigate, redirect, type RouteObject } from 'react-router-dom'
import GuesthouseDetailPage from '../features/accommodation/GuesthouseDetailPage'
import GuesthouseListPage from '../features/accommodation/GuesthouseListPage'
import BookingPage from '../features/booking/BookingPage'
import BookingRequestSuccessPage from '../features/booking/BookingRequestSuccessPage'
import { DEFAULT_LANGUAGE, readPreferredLanguage } from '../i18n/languages'
import LanguageLayout from './LanguageLayout'

export const appRoutes: RouteObject[] = [
  {
    path: '/',
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
