import { useTranslation } from 'react-i18next'
import { Link } from 'react-router-dom'
import type { Language } from '../../i18n/languages'

interface SiteFooterProps {
  language: Language
}

export default function SiteFooter({ language }: SiteFooterProps) {
  const { t } = useTranslation()

  return (
    <footer className="site-footer">
      <div className="site-footer-main">
        <div className="footer-brand-column">
          <div className="footer-logos">
            <img src="/images/logo/nisztor-logo.png" alt={t('homepage.hero.nisztorLogoAlt')} />
            <span aria-hidden="true" />
            <img src="/images/logo/bukovina-logo.png" alt={t('homepage.hero.bukovinaLogoAlt')} />
          </div>
        </div>

        <section className="footer-contact-column" aria-labelledby="footer-contact-heading">
          <h2 id="footer-contact-heading">{t('app.footer.contactTitle')}</h2>
          <a href="tel:+40743677812">+40 743 677 812</a>
          <a href="mailto:nisztorpanzio@gmail.com">nisztorpanzio@gmail.com</a>
          <address>{t('app.footer.address')}</address>
          <Link className="footer-booking-link" to={`/${language}/booking`}>
            {t('app.footer.bookingRequest')}
          </Link>
        </section>

        <nav className="footer-navigation" aria-label={t('app.footer.navigationTitle')}>
          <h2>{t('app.footer.navigationTitle')}</h2>
          <Link to={`/${language}`}>{t('app.footer.home')}</Link>
          <Link to={`/${language}/guesthouses/nisztor-panzio`}>
            {t('app.footer.nisztorGuesthouse')}
          </Link>
          <Link to={`/${language}/guesthouses/bukovina-panzio`}>
            {t('app.footer.bukovinaGuesthouse')}
          </Link>
          <Link to={`/${language}#contact`}>{t('app.footer.contact')}</Link>
        </nav>
      </div>
      <div className="site-footer-bottom">
        <p>
          © {new Date().getFullYear()} {t('app.footer.copyright')}
        </p>
      </div>
    </footer>
  )
}
