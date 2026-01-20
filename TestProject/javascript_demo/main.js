const { Calculator, globalAdd } = require('./calculator');

function main() {
    const calc = new Calculator();
    console.log("Add: " + calc.add(1, 2));
    console.log("Global Add: " + globalAdd(3, 4));
}

main();
