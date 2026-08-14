import Container from 'react-bootstrap/Container'
import { useTranslation } from 'react-i18next'
import { Link, useOutletContext, useParams } from 'react-router-dom'
import type { LanguageOutletContext } from '../../app/LanguageLayout'
import AsyncStatus from '../../shared/components/AsyncStatus'
import GuesthouseDetailContent from './GuesthouseDetailContent'
import { useGuesthouse } from './useGuesthouseData'

export default function GuesthouseDetailPage() {
  const { slug = '' } = useParams()
  const { language } = useOutletContext<LanguageOutletContext>()
  const { t } = useTranslation()
  const { data, loading, error } = useGuesthouse(slug, language)

  if (loading) {
    return (
      <Container as="main" fluid id="main-content" className="detail-status px-0">
        <AsyncStatus variant="loading" message={t('guesthouses.loading')} />
      </Container>
    )
  }

  if (error || !data) {
    return (
      <Container as="main" fluid id="main-content" className="detail-status px-0">
        <AsyncStatus variant="error" message={t('guesthouses.detailError')} />
        <Link className="text-link" to={`/${language}`}>
          ← {t('guesthouses.back')}
        </Link>
      </Container>
    )
  }

  return (
    <Container as="main" fluid id="main-content" className="guesthouse-detail px-0">
      <GuesthouseDetailContent data={data} language={language} />
    </Container>
  )
}
