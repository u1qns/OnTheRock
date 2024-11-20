import sys
import datetime
import json
import pymysql
import time
from pprint import pprint
from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from webdriver_manager.chrome import ChromeDriverManager
from bs4 import BeautifulSoup

# ChromeOptions 설정
chrome_options = webdriver.ChromeOptions()
chrome_options.add_argument("--headless")  # 헤드리스 모드
chrome_options.add_argument("--no-sandbox")
chrome_options.add_argument("--disable-dev-shm-usage")

# 혹시 cli 라고 생각해서 안해주나? 싶어서 추가
chrome_options.add_argument("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")  # User-Agent 설정

# ChromeDriver 자동 설치 및 초기화
service = Service(ChromeDriverManager(driver_version="120.0.6099.224").install())
driver = webdriver.Chrome(service=service, options=chrome_options)

def connect_to_db():
    return pymysql.connect(
        host='i11a801.p.ssafy.io',  # MariaDB 서버 주소
        port=3306,  # 포트 번호
        user='authadmin',  # 사용자 이름
        password='dlstkdlemdkdnt',  # 비밀번호
        database='contents',  # 데이터베이스 이름
        charset='utf8mb4',  # 문자 인코딩
        cursorclass=pymysql.cursors.DictCursor
    )

def create_gym_table():
    connection = connect_to_db()
    try:
        with connection.cursor() as cursor:
            create_table_query = """
            CREATE TABLE IF NOT EXISTS climbing_gyms (
                gym_id INT UNSIGNED PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                address VARCHAR(255) NOT NULL
            );
            """
            cursor.execute(create_table_query)
            connection.commit()  # 변경 사항 저장
    except Exception as e:
        print(f"테이블 생성 중 오류 발생: {e}")
    finally:
        connection.close()  # 연결 종료

def batch_insert_or_update_data(data_list):
    connection = connect_to_db()
    try:
        with connection.cursor() as cursor:
            sql = """
            INSERT INTO climbing_gyms (gym_id, name, address) 
            VALUES (%s, %s, %s) 
            ON DUPLICATE KEY UPDATE 
            name = VALUES(name), 
            address = VALUES(address)
            """
            for data in data_list:
                if 'id' in data and 'name' in data and 'fullAddress' in data:
                    cursor.execute(sql, (data['id'], data['name'], data['fullAddress']))
                else:
                    print(f"유효하지 않은 데이터: {data}")
            connection.commit()  # 변경 사항 저장
            print("데이터가 성공적으로 삽입 또는 업데이트되었습니다.")
    except Exception as e:
        print(f"데이터 삽입 중 오류 발생: {e}")
    finally:
        connection.close()  # 연결 종료

# 테이블 생성 호출
create_gym_table()

##################################################################################3

query = sys.argv[1]
driver.get(f"https://pcmap.place.naver.com/place/list?query={query}")

# 페이지 로딩을 위한 대기
timer = 10

# 크롤링
for p in range(20):
    time.sleep(timer)

    raw = driver.page_source
    html = BeautifulSoup(raw, "html.parser")
    apollo_state = driver.execute_script("return window.__APOLLO_STATE__;")

    # PlaceSummary 항목만 필터링
    place_summaries = {key: value for key, value in apollo_state.items() if key.startswith('PlaceSummary:')}

    # 필터링 및 필요한 데이터 추출
    filtered_data = [
        {
            "fullAddress": value["fullAddress"],
            "id": value["id"],
            "name": value["name"]
        }
        for key, value in place_summaries.items()
        if value.get("category") == '암벽등반'
    ]

    # 데이터베이스에 저장
    batch_insert_or_update_data(filtered_data)

    # 다음 페이지로 이동
    try:
        next_btn = WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.CSS_SELECTOR, "button.btn_next")))
        next_btn.click()
    except:
        break
    print("데이터 수집 완료")

driver.quit()