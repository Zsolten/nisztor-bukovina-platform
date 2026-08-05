import { useEffect, useState } from 'react'
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
      if (event.key === 'Escape') setSelectedIndex(null)
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

      {selectedImage && selectedIndex !== null && (
        <div
          className="lightbox"
          role="dialog"
          aria-modal="true"
          aria-label={selectedImage.altText}
        >
          <button
            className="lightbox-close"
            type="button"
            onClick={() => setSelectedIndex(null)}
            aria-label={t('guesthouses.closeGallery')}
          >
            ×
          </button>
          <button
            className="lightbox-arrow previous"
            type="button"
            onClick={() => setSelectedIndex((selectedIndex - 1 + images.length) % images.length)}
            aria-label={t('guesthouses.previousImage')}
          >
            ←
          </button>
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
          <button
            className="lightbox-arrow next"
            type="button"
            onClick={() => setSelectedIndex((selectedIndex + 1) % images.length)}
            aria-label={t('guesthouses.nextImage')}
          >
            →
          </button>
        </div>
      )}
    </>
  )
}
