import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL;

export const options = {
    vus: 1,
    duration: '5s',
};

export default function () {
    const res = http.get(`${BASE_URL}/swagger-ui/index.html`);
    check(res, {
        'swagger reachable': (r) => r.status === 200 || r.status === 302,
    });
    sleep(1);
}