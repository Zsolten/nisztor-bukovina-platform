interface FoundationScreenProps {
  title?: string
}

function FoundationScreen({ title = 'Nisztor-Bukovina Platform' }: FoundationScreenProps) {
  return (
    <main>
      <h1>{title}</h1>
    </main>
  )
}

export default FoundationScreen
