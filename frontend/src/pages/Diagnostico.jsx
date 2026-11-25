import { useEffect, useState } from 'react'
import cliente from '../api/client'

export default function Diagnostico() {
  const [salud, setSalud] = useState(null)

  useEffect(() => {
    cliente.get('/salud').then((r) => setSalud(r.data)).catch(() => {})

    // Cookie protegida (HttpOnly): la emite el backend al pedir la config de la
    // pasarela. No se puede leer con document.cookie; hay que interceptar la
    // respuesta con un proxy (Burp Suite) para ver la cabecera Set-Cookie.
    cliente.get('/pagos/configuracion').catch(() => {})
  }, [])

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      <h1>Diagnostico del sistema</h1>

      <section>
        <h2>Estado del servicio</h2>
        {salud && <pre>{JSON.stringify(salud, null, 2)}</pre>}
      </section>

      <section>
        <h2>Endpoints de Spring Boot Actuator</h2>
        {/* VULNERABILIDAD: actuator expone todos los endpoints sin autenticacion */}
        <p>
          <a href="/actuator" target="_blank" rel="noreferrer">
            /actuator
          </a>
          {' | '}
          <a href="/actuator/env" target="_blank" rel="noreferrer">
            /actuator/env
          </a>
          {' | '}
          <a href="/actuator/configprops" target="_blank" rel="noreferrer">
            /actuator/configprops
          </a>
          {' | '}
          <a href="/actuator/heapdump" target="_blank" rel="noreferrer">
            /actuator/heapdump
          </a>
        </p>
      </section>
    </div>
  )
}
