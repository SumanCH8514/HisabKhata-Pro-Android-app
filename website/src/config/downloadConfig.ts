export interface AppRelease {
  version: string
  buildNumber: number
  releaseDate: string
  apkSize: string
  apkSizeBytes: number
  minAndroid: string
  minSdk: number
  targetSdk: number
  architecture: string
  sha256: string
  downloadUrl: string
  releasesUrl: string
  repoUrl: string
  webAppUrl: string
  packageName: string
}

export const APP_CONFIG: AppRelease = {
  version: "1.2.0",
  buildNumber: 3,
  releaseDate: "November 2026",
  apkSize: "22.5 MB",
  apkSizeBytes: 23632896,
  minAndroid: "Android 8.0 (Oreo)+",
  minSdk: 26,
  targetSdk: 35,
  architecture: "Universal (arm64-v8a, armeabi-v7a, x86_64)",
  sha256: "E85F4BEC1C62A7848424A10452F8A40A61C96D38CA89BEC880FD41AAB30EB57C",
  downloadUrl: "https://github.com/SumanCH8514/HisabKhata-Pro-Android-app/raw/main/apk/app-debug.apk",
  releasesUrl: "https://github.com/SumanCH8514/HisabKhata-Pro-Android-app/releases",
  repoUrl: "https://github.com/SumanCH8514/HisabKhata-Pro-Android-app",
  webAppUrl: "https://hisabkhata.sumanonline.com/",
  packageName: "com.sumanonline.hisabkhata"
}
