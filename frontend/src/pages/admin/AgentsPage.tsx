import { useCallback, useEffect, useState } from 'react'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Chip from '@mui/material/Chip'
import Stack from '@mui/material/Stack'
import Tab from '@mui/material/Tab'
import Table from '@mui/material/Table'
import TableBody from '@mui/material/TableBody'
import TableCell from '@mui/material/TableCell'
import TableContainer from '@mui/material/TableContainer'
import TableHead from '@mui/material/TableHead'
import TableRow from '@mui/material/TableRow'
import Tabs from '@mui/material/Tabs'
import Paper from '@mui/material/Paper'
import { userService } from '../../services/user.service'
import { PageHeader } from '../../components/ui/PageHeader'
import { ErrorState, LoadingState } from '../../components/ui/StateViews'
import { UserStatusChip } from '../../components/ui/StatusChip'
import { useToast } from '../../context/ToastContext'
import { extractErrorMessage } from '../../lib/error'
import { formatDate } from '../../lib/format'
import type { User } from '../../types/user'

type TabKey = 'all' | 'pending'

export function AgentsPage() {
  const toast = useToast()
  const [tab, setTab] = useState<TabKey>('all')
  const [agents, setAgents] = useState<User[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [busyId, setBusyId] = useState<string | null>(null)

  const load = useCallback((which: TabKey) => {
    setError(null)
    setAgents(null)
    const request = which === 'pending' ? userService.getPendingAgents() : userService.getAllAgents()
    request
      .then(setAgents)
      .catch((err: unknown) => setError(extractErrorMessage(err)))
  }, [])

  useEffect(() => load(tab), [load, tab])

  const setStatus = async (user: User, status: 'ACTIVE' | 'SUSPENDED') => {
    setBusyId(user.id)
    try {
      const updated = await userService.updateStatus(user.id, status)
      toast.success(`${updated.name} is now ${status.toLowerCase()}.`)
      setAgents((prev) => (prev ? prev.map((a) => (a.id === updated.id ? updated : a)) : prev))
    } catch (err) {
      toast.error(extractErrorMessage(err))
    } finally {
      setBusyId(null)
    }
  }

  return (
    <>
      <PageHeader
        title="Agents"
        subtitle="Manage agent accounts and approve pending registrations."
        action={
          <Tabs value={tab} onChange={(_, value: TabKey) => setTab(value)}>
            <Tab label="All agents" value="all" />
            <Tab label="Pending (unverified)" value="pending" />
          </Tabs>
        }
      />
      {error ? (
        <ErrorState message={error} onRetry={() => load(tab)} />
      ) : !agents ? (
        <LoadingState label="Loading agents…" />
      ) : (
        <TableContainer component={Paper}>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Name</TableCell>
                <TableCell>Email</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Registered</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {agents.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5}>
                    <Box sx={{ py: 3, textAlign: 'center', color: 'text.secondary' }}>
                      No agents here.
                    </Box>
                  </TableCell>
                </TableRow>
              ) : (
                agents.map((agent) => (
                  <TableRow key={agent.id} hover>
                    <TableCell>{agent.name}</TableCell>
                    <TableCell>{agent.email}</TableCell>
                    <TableCell>
                      <UserStatusChip status={agent.status} />
                    </TableCell>
                    <TableCell>{formatDate(agent.createdAt)}</TableCell>
                    <TableCell align="right">
                      <Stack direction="row" spacing={1} sx={{ justifyContent: 'flex-end' }}>
                        {agent.status === 'UNVERIFIED' && (
                          <Button size="small" color="success" disabled={busyId === agent.id} onClick={() => setStatus(agent, 'ACTIVE')}>
                            Activate
                          </Button>
                        )}
                        {agent.status === 'ACTIVE' && (
                          <Button size="small" color="warning" disabled={busyId === agent.id} onClick={() => setStatus(agent, 'SUSPENDED')}>
                            Suspend
                          </Button>
                        )}
                        {agent.status === 'SUSPENDED' && (
                          <Button size="small" color="success" disabled={busyId === agent.id} onClick={() => setStatus(agent, 'ACTIVE')}>
                            Reactivate
                          </Button>
                        )}
                        {agent.status === 'ACTIVE' && <Chip size="small" label={agent.id} variant="outlined" sx={{ maxWidth: 180 }} title="Agent user ID (used when assigning to a property)" />}
                      </Stack>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </>
  )
}
