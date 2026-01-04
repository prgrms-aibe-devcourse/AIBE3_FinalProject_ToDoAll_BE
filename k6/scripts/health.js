import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL;

if (!BASE_URL) {
    throw new Error('BASE_URL 환경변수가 설정되지 않았습니다.');
}

export const options = {
    vus: 10,
    duration: '30s',
};

export default function () {
    const res = http.get(`${BASE_URL}/actuator/health`);

    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    sleep(1);
}