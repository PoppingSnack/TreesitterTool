class Calculator {
    constructor() {
        this.value = 0;
    }

    add(a, b) {
        return a + b;
    }

    subtract(a, b) {
        return a - b;
    }
}

function globalAdd(a, b) {
    return a + b;
}

module.exports = { Calculator, globalAdd };
