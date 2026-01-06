import { Player, PlayerNameComparator, PlayerNumberComparator } from './Player';

function generateRandomString(): string {
    return Math.random().toString(36).substring(2) + Date.now().toString(36);
}

export class Team {
    private teamId: string;
    private name: string;
    private players: Array<Player>;
    private isMixed: boolean;
    private isDisplayingPlayerNumber: boolean;

    constructor() {
        this.teamId = "team-" + generateRandomString();
        this.name = "";
        this.players = new Array<Player>();
        this.isMixed = false;
        this.isDisplayingPlayerNumber = false;
    }

    getTeamId(): string {
        return this.teamId;
    }

    setTeamId(teamId: string): void {
        this.teamId = teamId;
    }

    getName(): string {
        return this.name;
    }

    setName(name: string): void {
        this.name = name;
    }

    getIsMixed(): boolean {
        return this.isMixed;
    }

    setIsMixed(isMixed: boolean): void {
        this.isMixed = isMixed;
    }

    getIsDisplayingPlayerNumber(): boolean {
        return this.isDisplayingPlayerNumber;
    }

    setIsDisplayingPlayerNumber(isDisplayingPlayerNumber: boolean): void {
        this.isDisplayingPlayerNumber = isDisplayingPlayerNumber;
    }

    getPlayers(): Array<Player> {
        return this.players;
    }

    setPlayers(players: Array<Player>): void {
        this.players = players;
    }

    getPlayersSorted(): Array<Player> {
        return this.players.slice().sort(PlayerNameComparator);
    }

    getPlayer(playerName: string): Player | null {
        const lowerName = playerName.toLowerCase();
        return this.players.find(p => p.name.toLowerCase() === lowerName) || null;
    }

    addPlayer(player: Player): boolean {
        if (player.isAnonymous()) {
            return false;
        }
        const existingIndex = this.players.findIndex(p => p.name.toLowerCase() === player.name.toLowerCase());
        if (existingIndex !== -1) {
            this.players.splice(existingIndex, 1);
        }
        this.players.push(player);
        return existingIndex !== -1;
    }

    removePlayer(player: Player): boolean {
        const index = this.players.indexOf(player);
        if (index !== -1) {
            this.players.splice(index, 1);
            return true;
        }
        return false;
    }

    isDuplicatePlayerName(newPlayerName: string, playerToIgnore: Player | null): boolean {
        const lowerNewName = newPlayerName.toLowerCase();
        return this.players.some(p => p !== playerToIgnore && p.name.toLowerCase() === lowerNewName);
    }

    getDefaultLine(): Array<Player> {
        const candidates = this.players.filter(p => !p.isAnonymous());
        if (this.isMixed) {
            const males = candidates.filter(p => p.isMale);
            const females = candidates.filter(p => !p.isMale);
            const line: Player[] = [];
            let maleIndex = 0;
            let femaleIndex = 0;
            const maxPlayers = 7;
            for (let i = 0; i < maxPlayers && (maleIndex < males.length || femaleIndex < females.length); i++) {
                if (maleIndex < males.length && (femaleIndex >= females.length || i % 2 === 0)) {
                    line.push(males[maleIndex++]);
                } else if (femaleIndex < females.length) {
                    line.push(females[femaleIndex++]);
                }
            }
            return line;
        } else {
            return candidates.slice(0, 7);
        }
    }

    ensureValid(): void {
        this.players = this.players.filter(p => !p.isAnonymous());
    }

    hasCloudId(): boolean {
        return this.teamId !== "";
    }
}