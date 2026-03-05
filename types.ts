
export type Language = 'pt' | 'en' | 'es';

export enum Screen {
    Login,
    Main,
}

export enum Tab {
    Home,
    Settings,
}

export interface UserPosition {
    lat: number;
    lon: number;
}

export interface UserProfile {
    name: string;
    dob: string;
    disability: string;
    customDisability?: string;
    hasBracelet: boolean;
}

export interface BusData {
    distanceMeters?: number;
    velocityKmh?: string;
    eta?: string;
    rawBusData?: any;
    error?: string;
}

export interface RecentRoute {
    lineId: string;
    terminalId: string;
    timestamp: number;
}

export interface FavoriteRoute {
    lineId: string;
    terminalId: string;
}

export interface AICommand {
    action: 'NAVIGATE_HOME' | 'NAVIGATE_SETTINGS' | 'SELECT_TERMINAL' | 'TOGGLE_THEME' | 'READ_STATUS' | 'SELECT_ROUTE' | 'UNKNOWN';
    payload?: any;
    reply: string;
}
