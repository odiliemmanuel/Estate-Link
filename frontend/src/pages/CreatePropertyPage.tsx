import { useNavigate } from 'react-router-dom'
import { propertyService } from '../services/property.service'
import { PropertyForm } from '../components/property/PropertyForm'
import { PageHeader } from '../components/ui/PageHeader'
import { useToast } from '../context/ToastContext'
import { extractErrorMessage } from '../lib/error'
import type { CreatePropertyRequest } from '../types/property'

export function CreatePropertyPage() {
  const navigate = useNavigate()
  const toast = useToast()

  const handleSubmit = async (payload: CreatePropertyRequest) => {
    try {
      const created = await propertyService.create(payload)
      toast.success('Property created successfully.')
      navigate(`/properties/${created.id}`, { replace: true })
    } catch (err) {
      toast.error(extractErrorMessage(err))
      throw err
    }
  }

  return (
    <>
      <PageHeader title="Add a property" subtitle="The street address is verified against OpenStreetMap automatically." />
      <PropertyForm onSubmit={handleSubmit} />
    </>
  )
}
