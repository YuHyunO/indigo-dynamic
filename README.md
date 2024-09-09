<div align="left">
  <p>
    <h3>※개발 환경설정 정보</h3>
    <div>● JDK version: 1.7</div>
    <div>● MAVEN version: 3.8.8 / Download link: <a href="https://dlcdn.apache.org/maven/maven-3/3.8.8/binaries/apache-maven-3.8.8-bin.zip">apache-maven-3.8.8-bin.zip</a></div>
    <div>● MAVEN JVM option: -Dhttps.protocols=TLSv1.1,TLSv1.2</div>
  </p>
  
  <br>
  <p>
    <h3>※테스트 환경 정보</h3> 
    <div>[FTP/SFTP 서버 생성 및 관련설정 완료 (2024-09-07)]</div>
    <div>● FTP 접속정보: 52.78.68.15:7021 (oyh/oyh123)</div>
    <div>● SFTP 접속정보: 52.78.68.15:7022 (sftpuser1/sftpuser1, sftpuser2/sftpuser2)</div>
  </p>  
  
  <br>
  <p>
    <h3>※작업 노트</h3> 
    <div>◎ 최종 배포 시 logging level 조정, 불필요 로그 제거 및 주석 처리 작업 선행 ▶<i>[완료여부: X]</i></div>
    <div>◎ 리스트 파라미터가 전달되어 select문이 여러번 수행되어 결과가 합산되는 로직에서 DB서버와의 세션이 반복 생성되지 않도록 하나의 트랜잭션으로 처리 필요 ▶<i>[완료여부: O]</i> </div>
    <div>◎ 운영전환 시 동일한 어댑터에 속한 인터페이스이더라도 순차전환 할 수 있도록 인터페이스 활성화/비활성화 기능 필요.<br>→ 인터페이스 활성/비활성 HTTP API 어댑터 생성, 개별적인 어댑터에서는 스케줄링을 하면서 API 어댑터와 파일로 인터페이스 하여 서비스 인터페이스들을 활성/비활성 하는 방식. ▶<i>[완료여부: X]</i> </div>
    <div>◎ FTP 서버로 로그인하는 서비스에서 SeviceContext 내에 로그인 하려는 FTP 서버의 Session 이 이미 존재하는 경우 세션을 재생성하지 않도록 수정 필요. ▶<i>[완료여부: X]</i></div>
    <div>◎ Compiler 기능을 활용하여 Mapper 개발 ▶<i>[완료여부: X]</i></div>
  </p>
</div>
