import { Col, Container, Row } from 'react-bootstrap'
import { useTranslation } from 'react-i18next'
import { Link, useOutletContext } from 'react-router-dom'
import type { LanguageOutletContext } from '../../app/LanguageLayout'
import AsyncStatus from '../../shared/components/AsyncStatus'
import HomepageLegacy from './HomepageLegacy'
import HomepageHero from './HomepageHero'
import HomepageMap from './HomepageMap'
import HomepageReview from './HomepageReview'
import HomepageSurroundings from './HomepageSurroundings'
import { useGuesthouses } from './useGuesthouseData'

export default function GuesthouseListPage() {
  const { language } = useOutletContext<LanguageOutletContext>()
  const { t } = useTranslation()
  const { data, loading, error } = useGuesthouses(language)

  return (
    <main id="main-content">
      <HomepageHero />

      <Container
        as="section"
        fluid
        className="guesthouse-section"
        aria-labelledby="guesthouse-list-heading"
      >
        <header className="guesthouse-section-heading">
          <h2 id="guesthouse-list-heading">{t('guesthouses.sectionTitle')}</h2>
        </header>

        {loading && <AsyncStatus variant="loading" message={t('guesthouses.loading')} />}
        {error && <AsyncStatus variant="error" message={t('guesthouses.loadError')} />}

        {data && (
          <Row className="guesthouse-grid">
            {data.map((guesthouse, index) => (
              <Col className="guesthouse-column" xs={12} lg={6} key={guesthouse.slug}>
                <article className="guesthouse-card">
                  <Link
                    className="guesthouse-image-link"
                    to={`/${language}/guesthouses/${guesthouse.slug}`}
                    aria-label={`${guesthouse.name} – ${t('guesthouses.openDetails')}`}
                  >
                    <img
                      src={guesthouse.coverImage.path}
                      alt={guesthouse.coverImage.altText}
                      loading={index === 0 ? 'eager' : 'lazy'}
                    />
                  </Link>
                  <div className="guesthouse-card-copy">
                    <p className="room-count">
                      {t('guesthouses.roomCount', { count: guesthouse.roomCount })}
                    </p>
                    <h3>{guesthouse.name}</h3>
                    <Link className="text-link" to={`/${language}/guesthouses/${guesthouse.slug}`}>
                      {t('guesthouses.openDetails')}
                      <span aria-hidden="true">↗</span>
                    </Link>
                  </div>
                </article>
              </Col>
            ))}
          </Row>
        )}
      </Container>

      <HomepageLegacy />
      <HomepageSurroundings />
      <HomepageReview />
      <HomepageMap />
    </main>
  )
}
