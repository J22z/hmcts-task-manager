import { useEffect, useState } from 'react'
import './App.css'

// Format due dates using UK date and time conventions.
function formatDue(value) {
  return new Date(value).toLocaleString('en-GB', {
    dateStyle: 'medium',
    timeStyle: 'short'
  })
}

// Completed tasks are no longer treated as overdue.
function isOverdue(task) {
  return task.status !== 'COMPLETED' && new Date(task.dueDate) < new Date()
}

export default function App() {

  // Keep track of tasks, form inputs and any errors.
  const [tasks, setTasks] = useState([])
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [dueDate, setDueDate] = useState('')
  const [formError, setFormError] = useState('')
  const [listError, setListError] = useState('')
  const [saving, setSaving] = useState(false)
  const [busyIds, setBusyIds] = useState([])
  const [activeFilter, setActiveFilter] = useState('ALL')

  // Only show tasks that match the selected status.
  const visibleTasks =
      activeFilter === 'ALL'
          ? tasks
          : tasks.filter(task => task.status === activeFilter)

  // These totals stay up to date even when the list is filtered.
  const counts = {
    pending: tasks.filter(task => task.status === 'PENDING').length,
    inProgress: tasks.filter(task => task.status === 'IN_PROGRESS').length,
    completed: tasks.filter(task => task.status === 'COMPLETED').length
  }

  // Get the latest tasks from the backend.
  async function loadTasks() {
    try {
      const response = await fetch('/api/tasks')

      if (!response.ok) {
        throw new Error('Unable to load tasks.')
      }

      const data = await response.json()
      setTasks(data)
      setListError('')
    } catch (error) {
      setListError(error.message)
    }
  }

  // Load saved tasks when the page first opens.
  useEffect(() => {
    loadTasks()
  }, [])

  // Send the form details to the API and refresh the task list.
  async function createTask(event) {
    event.preventDefault()

    if (!title.trim() || !dueDate) {
      setFormError('Add a task title and due date to continue.')
      return
    }

    setSaving(true)
    setFormError('')

    try {
      const response = await fetch('/api/tasks', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          title: title.trim(),
          description: description.trim(),
          status: 'PENDING',
          dueDate
        })
      })

      if (!response.ok) {
        throw new Error('Could not create your task. Please try again.')
      }

      // Clear the form once the task has been saved.
      setTitle('')
      setDescription('')
      setDueDate('')

      await loadTasks()
    } catch (error) {
      setFormError(error.message)
    } finally {
      setSaving(false)
    }
  }

  // Update a task's status without changing its other details.
  async function updateStatus(id, status) {
    setBusyIds(current => [...current, id])

    try {
      const response = await fetch(`/api/tasks/${id}/status`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ status })
      })

      if (!response.ok) {
        throw new Error('Could not update the task status.')
      }

      await loadTasks()
    } catch (error) {
      setListError(error.message)
    } finally {
      setBusyIds(current => current.filter(taskId => taskId !== id))
    }
  }

  // Ask for confirmation before permanently deleting a task.
  async function deleteTask(id) {
    const confirmed = window.confirm(
        'Delete this task? This action cannot be undone.'
    )

    if (!confirmed) return

    setBusyIds(current => [...current, id])

    try {
      const response = await fetch(`/api/tasks/${id}`, {
        method: 'DELETE'
      })

      if (!response.ok) {
        throw new Error('Could not delete the task.')
      }

      await loadTasks()
    } catch (error) {
      setListError(error.message)
    } finally {
      setBusyIds(current => current.filter(taskId => taskId !== id))
    }
  }

  // Turn the API status values into labels for the interface.
  function statusLabel(status) {
    if (status === 'IN_PROGRESS') return 'In progress'
    if (status === 'COMPLETED') return 'Completed'
    return 'Pending'
  }

  return (
      <div className="app">
        <header className="page-header">
          <h1>Task Manager</h1>
          <p>Create, manage and track casework tasks.</p>
        </header>

        <main className="dashboard">

          {/* Form for creating a new task. */}
          <section className="panel" aria-labelledby="create-heading">
            <h2 id="create-heading">Create a task</h2>

            <p className="section-description">
              Keep the important details in one place.
            </p>

            <form onSubmit={createTask}>
              <div className="field">
                <label htmlFor="title">Task title *</label>
                <input
                    id="title"
                    type="text"
                    required
                    maxLength={200}
                    placeholder="What needs doing?"
                    value={title}
                    onChange={event => setTitle(event.target.value)}
                />
              </div>

              <div className="field">
                <label htmlFor="description">Description</label>
                <textarea
                    id="description"
                    maxLength={255}
                    placeholder="A little context, if useful"
                    value={description}
                    onChange={event => setDescription(event.target.value)}
                />
              </div>

              <div className="field">
                <label htmlFor="dueDate">Due date and time *</label>
                <input
                    id="dueDate"
                    type="datetime-local"
                    required
                    value={dueDate}
                    onChange={event => setDueDate(event.target.value)}
                />
              </div>

              {formError && (
                  <p className="error" role="alert">
                    {formError}
                  </p>
              )}

              <button type="submit" disabled={saving}>
                {saving ? 'Creating task…' : 'Create task'}
              </button>
            </form>
          </section>

          {/* Task list, including the status filters. */}
          <section className="panel" aria-labelledby="list-heading">
            <div className="list-heading">
              <div>
                <h2 id="list-heading">All tasks ({tasks.length})</h2>
                <p className="section-description">
                  Your work, at a glance.
                </p>
              </div>

              {activeFilter !== 'ALL' && (
                  <button
                      type="button"
                      className="show-all"
                      onClick={() => setActiveFilter('ALL')}
                  >
                    Show all
                  </button>
              )}
            </div>

            {/* Clicking a summary box filters the list below it. */}
            <div className="task-summary" aria-label="Filter tasks by status">

              <button
                  type="button"
                  className={`summary-item ${activeFilter === 'PENDING' ? 'selected' : ''}`}
                  onClick={() => setActiveFilter('PENDING')}
                  aria-pressed={activeFilter === 'PENDING'}
              >
                <span className="summary-count">{counts.pending}</span>
                <span className="summary-label">Pending</span>
              </button>

              <button
                  type="button"
                  className={`summary-item ${activeFilter === 'IN_PROGRESS' ? 'selected' : ''}`}
                  onClick={() => setActiveFilter('IN_PROGRESS')}
                  aria-pressed={activeFilter === 'IN_PROGRESS'}
              >
                <span className="summary-count">{counts.inProgress}</span>
                <span className="summary-label">In progress</span>
              </button>

              <button
                  type="button"
                  className={`summary-item ${activeFilter === 'COMPLETED' ? 'selected' : ''}`}
                  onClick={() => setActiveFilter('COMPLETED')}
                  aria-pressed={activeFilter === 'COMPLETED'}
              >
                <span className="summary-count">{counts.completed}</span>
                <span className="summary-label">Completed</span>
              </button>

            </div>

            {listError && (
                <p className="error" role="alert">
                  {listError}
                </p>
            )}

            {/* Show a different message when the selected category is empty. */}
            {visibleTasks.length === 0 ? (
                <div className="empty-state">
                  <div className="empty-icon" aria-hidden="true">
                    <span />
                    <span />
                    <span />
                  </div>

                  <h3>
                    {activeFilter === 'ALL'
                        ? 'Nothing on your list. Yet.'
                        : 'No tasks in this category.'}
                  </h3>

                  <p>
                    {activeFilter === 'ALL'
                        ? 'Create your first task to get things moving.'
                        : 'Choose another status or show all tasks.'}
                  </p>
                </div>
            ) : (
                <ul className="task-list">
                  {visibleTasks.map(task => {
                    const overdue = isOverdue(task)
                    const busy = busyIds.includes(task.id)

                    return (
                        <li
                            key={task.id}
                            className={`task ${
                                task.status === 'COMPLETED' ? 'done' : ''
                            } ${overdue ? 'overdue' : ''}`}
                        >
                          <div className="task-body">
                            <div className="task-status-line">
                        <span
                            className={`status-dot status-${task.status.toLowerCase()}`}
                            aria-hidden="true"
                        />

                              <span className="status-text">
                          {statusLabel(task.status)}
                        </span>
                            </div>

                            <h3>{task.title}</h3>

                            {task.description && (
                                <p className="task-description">
                                  {task.description}
                                </p>
                            )}

                            <p className="due">
                              {overdue ? 'Overdue: ' : 'Due '}
                              {formatDue(task.dueDate)}
                            </p>
                          </div>

                          <div className="actions">
                            <select
                                aria-label={`Status for ${task.title}`}
                                value={task.status}
                                disabled={busy}
                                onChange={event =>
                                    updateStatus(task.id, event.target.value)
                                }
                            >
                              <option value="PENDING">Pending</option>
                              <option value="IN_PROGRESS">In progress</option>
                              <option value="COMPLETED">Completed</option>
                            </select>

                            <button
                                type="button"
                                className="delete"
                                disabled={busy}
                                onClick={() => deleteTask(task.id)}
                                aria-label={`Delete ${task.title}`}
                            >
                              Delete
                            </button>
                          </div>
                        </li>
                    )
                  })}
                </ul>
            )}
          </section>
        </main>

        <footer className="page-footer">
          <span>Task Manager</span>
          <span>Simple work. Clear progress.</span>
        </footer>
      </div>
  )
}