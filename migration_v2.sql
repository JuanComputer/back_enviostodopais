-- ═══════════════════════════════════════════════════════════
-- MIGRACIÓN v2 — EnviosTodoPaís
-- Ejecutar en Neon antes de desplegar el backend v2
-- ═══════════════════════════════════════════════════════════

-- 1. Agregar sede a usuarios (para Operadores y Admins de Sede)
ALTER TABLE tb_usuarios
  ADD COLUMN IF NOT EXISTS tb_tiendas_id UUID REFERENCES tb_tiendas(tb_tiendas_id);

-- 2. Nuevos campos en tb_envios
ALTER TABLE tb_envios
  ADD COLUMN IF NOT EXISTS tb_envios_nota_estado         VARCHAR(500),
  ADD COLUMN IF NOT EXISTS tb_envios_origen_tienda_id    UUID REFERENCES tb_tiendas(tb_tiendas_id),
  ADD COLUMN IF NOT EXISTS tb_envios_registrado_por_id   UUID REFERENCES tb_usuarios(tb_usuarios_id),
  ADD COLUMN IF NOT EXISTS tb_envios_peso                NUMERIC(8,2),
  ADD COLUMN IF NOT EXISTS tb_envios_valor_declarado     NUMERIC(10,2),
  ADD COLUMN IF NOT EXISTS tb_envios_tipo_servicio       VARCHAR(30),
  ADD COLUMN IF NOT EXISTS tb_envios_emisor_razon_social VARCHAR(255),
  ADD COLUMN IF NOT EXISTS tb_envios_receptor_razon_social VARCHAR(255),
  ADD COLUMN IF NOT EXISTS tb_envios_tipo_documento      VARCHAR(20),
  ADD COLUMN IF NOT EXISTS tb_envios_numero_documento    VARCHAR(30),
  ADD COLUMN IF NOT EXISTS tb_envios_precio_envio        NUMERIC(10,2),
  ADD COLUMN IF NOT EXISTS tb_envios_descripcion_paquete VARCHAR(500);

-- 3. Ampliar receptor_dni a 11 caracteres (para RUC)
ALTER TABLE tb_envios
  ALTER COLUMN tb_envios_receptor_dni TYPE VARCHAR(11);

-- 4. Actualizar estados existentes a "Registrado" si estaban en "En tránsito"
UPDATE tb_envios SET tb_envios_estado = 'Registrado'
WHERE tb_envios_estado = 'En tránsito';

-- 5. Roles nuevos (insertar si no existen)
INSERT INTO tb_roles (tb_roles_id, tb_roles_nombre, tb_roles_fecha_creacion)
VALUES
  (gen_random_uuid(), 'Administrador General',  NOW()),
  (gen_random_uuid(), 'Administrador de Sede',  NOW()),
  (gen_random_uuid(), 'Operador',               NOW()),
  (gen_random_uuid(), 'Cliente',                NOW())
ON CONFLICT (tb_roles_nombre) DO NOTHING;
