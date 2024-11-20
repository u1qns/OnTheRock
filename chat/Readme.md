### json 형식으로 메세지 전달
{
"chatUser": "chanmin",
"message": "그쪽에 보여??",
"messageTime": "",
"messageAction": "MESSAGE",
"chatRoomId": ""
}

### 연결주소
ws://localhost:8080/ws \
채팅방 구독: /chat/sub/{chatRoomId} \
채팅 전송 : /chat/pub  \
사용자 추가: /chat/addUser/{chatRoomId} \
사용자 퇴장 알림: WebSocket 세션 종료 이벤트 리스너

채팅기록 삭제(DELETE): http://localhost:8080/rooms/1 \
채팅기록 조회(GET): http://localhost:8080/rooms/1 \
이전채팅 더보기(GET): http://localhost:8080/rooms/2/messages?size=10&before=2024-07-23T04:35:47.000Z \
채팅기록 다운로드(GET) : http://localhost:8080/rooms/1/download





테스트123456789101111211111122