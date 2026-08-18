import { House, RefreshCw } from 'lucide-react'
import { Link, useLocation, useParams } from 'react-router-dom'
import { DEFAULT_LANGUAGE, isSupportedLanguage } from '../i18n/languages'
import { resources } from '../i18n/resources'

export default function ApplicationErrorPage() {
  const { lang } = useParams()
  const location = useLocation()
  const language = isSupportedLanguage(lang) ? lang : DEFAULT_LANGUAGE
  const messages = resources[language].translation.app.error
  const isAdminPage = location.pathname.startsWith('/admin')
  const returnTo = isAdminPage ? '/admin/login' : `/${language}`

  return (
    <main className="application-error-page" id="main-content">
      <section className="application-error-content" aria-labelledby="application-error-title">
        <p className="eyebrow">{messages.eyebrow}</p>
        <h1 id="application-error-title">{messages.title}</h1>
        <p>{messages.description}</p>
        <div className="application-error-actions">
          <button type="button" onClick={() => window.location.reload()}>
            <RefreshCw aria-hidden="true" size={17} />
            {messages.retry}
          </button>
          <Link to={returnTo}>
            <House aria-hidden="true" size={17} />
            {isAdminPage ? messages.adminReturn : messages.homeReturn}
          </Link>
        </div>
      </section>
    </main>
  )
}
