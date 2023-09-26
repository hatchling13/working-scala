# 파일 입출력
## 개요
본 프로젝트는 os-lib를 활용하여 파일 시스템을 쉽게 다루는 방법에 대해 실습합니다.

## 의존성
```sbt
"com.lihaoyi" %% "os-lib" % "0.9.1"
```

## 멤버 및 메소드 목록
### os.pwd
- 현재 작업 디렉토리를 나타냅니다.
- `os.pwd / "폴더명"` 형식으로 활용할 수 있습니다.
### os.read(arg: ReadablePath)
- 파일 경로에 있는 파일을 읽어옵니다.
### os.write(target: Path, data: String)
- 파일을 생성합니다.
- target에 동일한 파일이 있으면 `FileAlreadyExistsException`이 발생합니다.
- 동일한 파일이 있을 때 덮어쓰고 싶을 경우 `os.write.over`를 사용할 수 있습니다.
