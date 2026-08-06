import Col from 'react-bootstrap/Col'
import Row from 'react-bootstrap/Row'
import { useTranslation } from 'react-i18next'
import type { GuesthouseAddress, GuesthouseContact as Contact } from '../../shared/api/guesthouses'

interface GuesthouseContactProps {
  contacts: Contact[]
  address: GuesthouseAddress
}

function contactHref(contact: Contact) {
  if (contact.type === 'PHONE') return `tel:${contact.value.replace(/\s+/g, '')}`
  if (contact.type === 'EMAIL') return `mailto:${contact.value}`
  return undefined
}

export default function GuesthouseContact({ contacts, address }: GuesthouseContactProps) {
  const { t } = useTranslation()
  const mapHref = `https://www.google.com/maps/search/?api=1&query=${address.latitude},${address.longitude}`

  return (
    <section
      className="detail-sheet detail-sheet--left detail-sheet--dark contact-sheet"
      aria-labelledby="contact-heading"
    >
      <header className="detail-sheet-heading">
        <p className="section-index">06</p>
        <h2 id="contact-heading">{t('guesthouses.contact')}</h2>
      </header>
      <Row className="contact-grid">
        <Col xs={12} lg={7}>
          <div className="contact-list">
            {contacts.map((contact) => {
              const href = contactHref(contact)
              const content = (
                <>
                  <span>
                    {contact.label}
                    {contact.preferred && <small>{t('guesthouses.preferredPhone')}</small>}
                  </span>
                  <strong>{contact.value}</strong>
                </>
              )

              return href ? (
                <a href={href} key={`${contact.type}-${contact.value}`}>
                  {content}
                </a>
              ) : (
                <div key={`${contact.type}-${contact.value}`}>{content}</div>
              )
            })}
          </div>
        </Col>
        <Col xs={12} lg={5}>
          <address className="address-card">
            <span>{t('guesthouses.address')}</span>
            <strong>{address.formatted}</strong>
            <a href={mapHref} target="_blank" rel="noreferrer">
              {t('guesthouses.openMap')} <span aria-hidden="true">↗</span>
            </a>
          </address>
        </Col>
      </Row>
    </section>
  )
}
