type SectionHeaderProps = {
  eyebrow?: string
  title: string
  lead?: string
}

export function SectionHeader({ eyebrow, title, lead }: SectionHeaderProps) {
  return (
    <header className="section-header">
      {eyebrow ? <span className="eyebrow">{eyebrow}</span> : null}
      <h1 className="section-title">{title}</h1>
      {lead ? <p className="lead">{lead}</p> : null}
    </header>
  )
}
