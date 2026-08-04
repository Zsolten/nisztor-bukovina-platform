import { createBrowserRouter, RouterProvider } from 'react-router-dom'
import { AppProviders } from './providers'
import { appRoutes } from './router'

const router = createBrowserRouter(appRoutes)

function App() {
  return (
    <AppProviders>
      <RouterProvider router={router} />
    </AppProviders>
  )
}

export default App
