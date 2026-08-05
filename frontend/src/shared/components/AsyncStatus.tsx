import Alert from 'react-bootstrap/Alert'
import Spinner from 'react-bootstrap/Spinner'

interface AsyncStatusProps {
  variant: 'loading' | 'error'
  message: string
}

export default function AsyncStatus({ variant, message }: AsyncStatusProps) {
  if (variant === 'error') {
    return (
      <Alert className="async-status" variant="danger">
        {message}
      </Alert>
    )
  }

  return (
    <div className="async-status" role="status">
      <Spinner animation="border" size="sm" aria-hidden="true" />
      <span>{message}</span>
    </div>
  )
}
