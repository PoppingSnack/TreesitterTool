import { UserService } from './user_service';

function main() {
    const service = new UserService();
    service.addUser({ id: 1, name: 'Alice' });
    console.log(service.getUsers());
}

main();
