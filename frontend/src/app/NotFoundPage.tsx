import { ArrowLeft, House } from 'lucide-react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { DEFAULT_LANGUAGE, isSupportedLanguage } from '../i18n/languages'
import { resources } from '../i18n/resources'

export default function NotFoundPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const firstPathSegment = location.pathname.split('/').filter(Boolean)[0]
  const language = isSupportedLanguage(firstPathSegment) ? firstPathSegment : DEFAULT_LANGUAGE
  const messages = resources[language].translation.app.notFound
  const isAdminPage = location.pathname.startsWith('/admin')
  const returnTo = isAdminPage ? '/admin/login' : `/${language}`

  return (
    <main className="application-error-page" id="main-content">
      <section className="application-error-content" aria-labelledby="not-found-title">
        <p className="eyebrow">{messages.eyebrow}</p>
        <h1 id="not-found-title">{messages.title}</h1>
        <p>{messages.description}</p>
        <div className="application-error-actions">
          <button type="button" onClick={() => navigate(-1)}>
            <ArrowLeft aria-hidden="true" size={17} />
            {messages.back}
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
