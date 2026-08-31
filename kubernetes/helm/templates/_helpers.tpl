{{/*
Basisname des Charts
*/}}
{{- define "app.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Voll qualifizierter App-Name (Release + Chart), wird als Prefix fuer Resourcen verwendet.
*/}}
{{- define "app.fullname" -}}
{{- if .Values.fullnameOverride -}}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- $name := default .Chart.Name .Values.nameOverride -}}
{{- if contains $name .Release.Name -}}
{{- .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Chart-Name inkl. Version, fuer das "helm.sh/chart" Label
*/}}
{{- define "app.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Gemeinsame Labels
*/}}
{{- define "app.labels" -}}
helm.sh/chart: {{ include "app.chart" . }}
app.kubernetes.io/part-of: {{ include "app.name" . }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}

{{/*
Selector-Labels fuer eine Teilkomponente (component: backend|frontend|postgres).
KORREKTUR K1: Der Aufrufer uebergibt den Root-Kontext explizit als "root"
(dict "root" . "component" "backend"), damit app.fullname innerhalb dieses
Helpers wieder Zugriff auf .Values/.Chart/.Release hat. Der bisherige Aufruf
(dict "component" ... "Release" .Release) liess app.fullname mit einem
Kontext ohne .Values/.Chart laufen -> "nil pointer"-Fehler bei helm template.
*/}}
{{- define "app.selectorLabels" -}}
app.kubernetes.io/name: {{ include "app.fullname" .root }}-{{ .component }}
app.kubernetes.io/instance: {{ .root.Release.Name }}
{{- end -}}
