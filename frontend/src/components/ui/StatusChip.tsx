import Chip from '@mui/material/Chip'
import { titleCase } from '../../lib/format'
import type { AvailabilityStatus, ListingStatus, Purpose } from '../../types/property'
import type { Role, UserStatus } from '../../types/user'

type ChipProps = {
  label: string
  color?: 'default' | 'primary' | 'secondary' | 'error' | 'info' | 'success' | 'warning'
}

function StatusChip({ label, color }: ChipProps) {
  return <Chip size="small" label={label} color={color ?? 'default'} variant="outlined" />
}

export function UserStatusChip({ status }: { status: UserStatus }) {
  const color: ChipProps['color'] =
    status === 'ACTIVE' ? 'success' : status === 'SUSPENDED' ? 'error' : 'warning'
  return <StatusChip label={titleCase(status)} color={color} />
}

export function AvailabilityStatusChip({ status }: { status: AvailabilityStatus }) {
  const color: ChipProps['color'] =
    status === 'AVAILABLE'
      ? 'success'
      : status === 'LEASED' || status === 'SOLD'
        ? 'info'
        : 'warning'
  return <StatusChip label={titleCase(status)} color={color} />
}

export function ListingStatusChip({ status }: { status: ListingStatus }) {
  const color: ChipProps['color'] =
    status === 'ACTIVE'
      ? 'success'
      : status === 'PENDING_APPROVAL'
        ? 'warning'
        : status === 'REJECTED' || status === 'SUSPENDED'
          ? 'error'
          : 'default'
  return <StatusChip label={titleCase(status)} color={color} />
}

export function PurposeChip({ purpose }: { purpose: Purpose }) {
  return (
    <StatusChip
      label={purpose === 'FOR_RENT' ? 'For rent' : 'For sale'}
      color={purpose === 'FOR_RENT' ? 'primary' : 'secondary'}
    />
  )
}

export function RoleChip({ role }: { role: Role }) {
  const color: ChipProps['color'] =
    role === 'ADMIN' ? 'error' : role === 'AGENT' ? 'secondary' : role === 'OWNER' ? 'primary' : 'default'
  return <StatusChip label={titleCase(role)} color={color} />
}
