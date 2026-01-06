export enum PlayerPosition {
    Any = "Any",
    Cutter = "Cutter",
    Handler = "Handler"
}

export class Player {
    private static readonly ANON_NAME = "Anonymous";
    private static anonymousPlayer: Player;

    name: string;
    number: string;
    isMale: boolean;
    leaguevineJson: string;
    position: PlayerPosition;
    isAbsent: boolean;

    static {
        Player.anonymousPlayer = new Player();
        Player.anonymousPlayer.name = Player.ANON_NAME;
    }

    constructor();
    constructor(name: string);
    constructor(name?: string) {
        this.name = name ?? "";
        this.number = "";
        this.position = PlayerPosition.Any;
        this.isMale = true;
        this.leaguevineJson = "";
        this.isAbsent = false;
    }

    static anonymous(): Player {
        return Player.anonymousPlayer;
    }

    isAnonymous(): boolean {
        return Player.ANON_NAME === this.name;
    }

    getId(): string {
        return this.name;
    }

    setName(name: string): void {
        if (this.name !== Player.ANON_NAME && name !== Player.ANON_NAME) {
            this.name = name;
        }
    }

    getPlayerNumberDescription(): string {
        return this.number === null || this.number === "" ? this.name : this.number;
    }

    get isFemale(): boolean {
        return !this.isAnonymous() && !this.isMale;
    }

    isDuplicateNewName(newName: string, players: Player[]): boolean {
        if (newName.toLowerCase() === this.name.toLowerCase()) {
            return false;
        } else {
            return players.some(player => player.name.toLowerCase() === newName.toLowerCase());
        }
    }

    hashCode(): number {
        const prime = 31;
        let result = 1;
        result = prime * result + (this.name ? this.name.toLowerCase().split('').reduce((a, b) => {
            a = ((a << 5) - a) + b.charCodeAt(0);
            return a & a;
        }, 0) : 0);
        return result;
    }

    equals(obj: any): boolean {
        if (this === obj) return true;
        if (!obj || !(obj instanceof Player)) return false;
        return this.name ? this.name.toLowerCase() === obj.name.toLowerCase() : !obj.name;
    }

    toString(): string {
        return `Player [name=${this.name}]`;
    }
}

export function PlayerNameComparator(player1: Player, player2: Player): number {
    return player1.name.localeCompare(player2.name);
}

export function PlayerNumberComparator(player1: Player, player2: Player): number {
    const player1Number = getNumber(player1.number);
    const player2Number = getNumber(player2.number);
    return player1Number - player2Number;
}

function getNumber(numberAsString: string): number {
    if (!numberAsString || numberAsString.trim() === "") {
        return 0;
    }
    const parsed = parseInt(numberAsString, 10);
    return isNaN(parsed) ? 0 : parsed;
}