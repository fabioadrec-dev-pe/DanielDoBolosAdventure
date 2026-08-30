#!/usr/bin/env bash
# Empacota o jogo para Windows (EXE Packr + JRE embarcada). Nao precisa de JDK no PC do jogador.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
VERSION="${1:-1.0.2}"
CACHE="${ROOT}/build/windows-tools"
PKG_ROOT="${ROOT}/build/windows-package"
APP_DIR="${PKG_ROOT}/DanielDoBolosAdventure"
DIST_DIR="${ROOT}/dist"
JAR_NAME="DanielDoBolosAdventure-${VERSION}-all.jar"
JAR_SRC="${ROOT}/desktop/build/libs/${JAR_NAME}"
ZIP_OUT="${DIST_DIR}/DanielDoBolosAdventure-Windows-${VERSION}.zip"
PACKR_JAR="${CACHE}/packr-all-4.1.0.jar"
PACKR_URL="https://repo1.maven.org/maven2/com/bladecoder/packr/packr-all/4.1.0/packr-all-4.1.0.jar"
JRE_URL="https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jre/hotspot/normal/eclipse"
JRE_ZIP="${CACHE}/jre-win.zip"

mkdir -p "${CACHE}" "${DIST_DIR}"

if [[ ! -f "${JAR_SRC}" ]]; then
  echo "JAR nao encontrado: ${JAR_SRC}" >&2
  echo "Rode antes: ./gradlew :desktop:shadowJar" >&2
  exit 1
fi

if [[ ! -f "${PACKR_JAR}" ]]; then
  echo "Baixando Packr..."
  curl -fsSL -o "${PACKR_JAR}" "${PACKR_URL}"
fi

if [[ ! -f "${JRE_ZIP}" ]]; then
  echo "Baixando Temurin JRE 17 (Windows x64)..."
  curl -fsSL -L -o "${JRE_ZIP}" "${JRE_URL}"
fi

rm -rf "${PKG_ROOT}"
mkdir -p "${PKG_ROOT}"

echo "Gerando pacote Packr (windows64)..."
java -jar "${PACKR_JAR}" \
  --platform windows64 \
  --jdk "${JRE_ZIP}" \
  --executable DanielDoBolosAdventure \
  --classpath "${JAR_SRC}" \
  --mainclass com.fabioad.ddba.app.DesktopLauncher \
  --vmargs Xms128m Xmx512m \
  --output "${APP_DIR}"

# Fallback .cmd (se o .exe for bloqueado pelo SmartScreen)
cat > "${APP_DIR}/DanielDoBolosAdventure.cmd" <<EOF
@echo off
cd /d "%~dp0"
title Daniel do Bolo's Adventure
if not exist "jre\\bin\\javaw.exe" (
  echo JRE embarcada nao encontrada. Extraia a pasta inteira do ZIP.
  pause
  exit /b 1
)
"jre\\bin\\javaw.exe" -Xms128m -Xmx512m -cp "${JAR_NAME}" com.fabioad.ddba.app.DesktopLauncher 1>"erro.log" 2>&1
if errorlevel 1 (
  echo.
  echo Falhou ao iniciar. Tentando modo console...
  "jre\\bin\\java.exe" -Xms128m -Xmx512m -cp "${JAR_NAME}" com.fabioad.ddba.app.DesktopLauncher
  echo.
  echo Se ainda falhou, abra o arquivo erro.log nesta pasta.
  pause
)
EOF

cat > "${APP_DIR}/LEIA-ME.txt" <<'EOF'
Daniel do Bolo's Adventure — Windows (portátil)

NÃO precisa instalar Java.

1. Extraia a pasta inteira do ZIP (não rode de dentro do ZIP).
2. Abra DanielDoBolosAdventure.exe

Se o Windows disser "O Windows protegeu o PC" (SmartScreen):
  - Clique em "Mais informações" -> "Executar assim mesmo"

Se o .exe não abrir:
  - Use DanielDoBolosAdventure.cmd
  - Veja erro.log se for criado

Mantenha sempre: .exe/.cmd + .jar + pasta jre + .json juntos.
EOF

# Icone Windows (EXE/pasta)
if [[ -f "${ROOT}/assets/branding/icon.ico" ]]; then
  cp -f "${ROOT}/assets/branding/icon.ico" "${APP_DIR}/icon.ico"
elif command -v convert >/dev/null 2>&1; then
  convert "${ROOT}/assets/branding/icon_512.png" -define icon:auto-resize=256,128,64,48,32,16 "${APP_DIR}/icon.ico" 2>/dev/null || true
fi

# Tenta gravar o icone no .exe (rcedit via wine, se disponivel)
RCEDIT="${CACHE}/rcedit-x64.exe"
if [[ ! -f "${RCEDIT}" ]]; then
  curl -fsSL -o "${RCEDIT}" "https://github.com/electron/rcedit/releases/download/v2.0.0/rcedit-x64.exe" 2>/dev/null || true
fi
if [[ -f "${RCEDIT}" && -f "${APP_DIR}/icon.ico" ]] && command -v wine64 >/dev/null 2>&1; then
  WINEDEBUG=-all wine64 "${RCEDIT}" "${APP_DIR}/DanielDoBolosAdventure.exe" --set-icon "${APP_DIR}/icon.ico" 2>/dev/null || true
fi

test -f "${APP_DIR}/DanielDoBolosAdventure.exe" || { echo "EXE nao gerado" >&2; exit 1; }
test -f "${APP_DIR}/jre/bin/javaw.exe" || { echo "JRE incompleta" >&2; exit 1; }

cd "${PKG_ROOT}"
rm -f "${ZIP_OUT}"
zip -r -q "${ZIP_OUT}" DanielDoBolosAdventure

echo "OK: ${ZIP_OUT}"
ls -lh "${ZIP_OUT}"
file "${APP_DIR}/DanielDoBolosAdventure.exe"
