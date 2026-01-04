import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL;

export const options = {
    vus: 1,
    duration: '5s',
};

export default function () {
    const res = http.get(`${BASE_URL}/actuator/health`);
    check(res, {
        'actuator reachable': (r) => r.status === 200,
    });
    sleep(1);
}