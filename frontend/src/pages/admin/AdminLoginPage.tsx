import type { FormEvent } from 'react'
import { useTranslation } from 'react-i18next'
import { SectionHeader } from '../../components/SectionHeader'

export function AdminLoginPage() {
  const { t } = useTranslation()

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    event.currentTarget.reset()
  }

  return (
    <>
      <SectionHeader title={t('admin.login')} lead={t('admin.lead')} />
      <form className="form-panel form-grid" onSubmit={handleSubmit}>
        <label>
          {t('admin.email')}
          <input name="email" type="email" autoComplete="email" required />
        </label>
        <label>
          {t('admin.password')}
          <input name="password" type="password" autoComplete="current-password" required />
        </label>
        <div className="full-field">
          <button className="button-action" type="submit">
            {t('admin.signIn')}
          </button>
        </div>
      </form>
    </>
  )
}
