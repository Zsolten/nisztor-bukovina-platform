import { useTranslation } from 'react-i18next'
import { Link } from 'react-router-dom'
import { localizedPath } from '../../routing/localizedPath'
import { useActiveLanguage } from '../../routing/useActiveLanguage'

export function NotFoundPage() {
  const { t } = useTranslation()
  const language = useActiveLanguage()

  return (
    <section className="not-found">
      <div>
        <h1>{t('common.notFound')}</h1>
        <Link className="primary-action" to={localizedPath(language)}>
          {t('common.backHome')}
        </Link>
      </div>
    </section>
  )
}
