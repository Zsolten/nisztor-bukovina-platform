import { useEffect, useState } from 'react'
import Button from 'react-bootstrap/Button'
import Modal from 'react-bootstrap/Modal'
import { useTranslation } from 'react-i18next'
import type { GuesthouseImage } from '../../shared/api/guesthouses'

interface GuesthouseGalleryProps {
  images: GuesthouseImage[]
}

export default function GuesthouseGallery({ images }: GuesthouseGalleryProps) {
  const { t } = useTranslation()
  const [selectedIndex, setSelectedIndex] = useState<number | null>(null)
  const selectedImage = selectedIndex === null ? null : images[selectedIndex]

  useEffect(() => {
    if (selectedIndex === null) return

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'ArrowLeft') {
        setSelectedIndex((current) =>
          current === null ? null : (current - 1 + images.length) % images.length,
        )
      }
      if (event.key === 'ArrowRight') {
        setSelectedIndex((current) => (current === null ? null : (current + 1) % images.length))
      }
    }

    document.addEventListener('keydown', handleKeyDown)
    return () => document.removeEventListener('keydown', handleKeyDown)
  }, [images.length, selectedIndex])

  return (
    <>
      <div className="gallery-grid">
        {images.map((image, index) => (
          <button
            className="gallery-item"
            key={image.path}
            type="button"
            onClick={() => setSelectedIndex(index)}
            aria-label={image.altText}
          >
            <img src={image.path} alt={image.altText} loading="lazy" />
            <span aria-hidden="true">{String(index + 1).padStart(2, '0')}</span>
          </button>
        ))}
      </div>

      <Modal
        show={selectedIndex !== null}
        onHide={() => setSelectedIndex(null)}
        centered
        size="xl"
        fullscreen="sm-down"
        className="gallery-modal"
        aria-label={selectedImage?.altText}
      >
        <Modal.Header closeButton closeLabel={t('guesthouses.closeGallery')} />
        {selectedImage && selectedIndex !== null && (
          <Modal.Body>
            <Button
              className="gallery-modal-arrow previous"
              type="button"
              variant="outline-light"
              onClick={() => setSelectedIndex((selectedIndex - 1 + images.length) % images.length)}
              aria-label={t('guesthouses.previousImage')}
            >
              ←
            </Button>
            <figure>
              <img src={selectedImage.path} alt={selectedImage.altText} />
              <figcaption>
                <span>{selectedImage.altText}</span>
                <span>
                  {t('guesthouses.imageCounter', {
                    current: selectedIndex + 1,
                    total: images.length,
                  })}
                </span>
              </figcaption>
            </figure>
            <Button
              className="gallery-modal-arrow next"
              type="button"
              variant="outline-light"
              onClick={() => setSelectedIndex((selectedIndex + 1) % images.length)}
              aria-label={t('guesthouses.nextImage')}
            >
              →
            </Button>
          </Modal.Body>
        )}
      </Modal>
    </>
  )
}
